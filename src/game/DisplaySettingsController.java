package game;

import javax.swing.JFrame;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//======================================================================================================================
// settings에 있던 디스플레이 관련 코드들 전부 여기로 빼둠, ai로 리팩토링 다시 함
//======================================================================================================================
final class DisplaySettingsController {
    private GraphicsDevice changedDevice;
    private DisplayMode originalMode;
    private boolean restoreOnCloseInstalled;

    void apply(JFrame frame, String mode, String resolution) {
        int[] size = parseResolution(resolution);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        boolean wasFullscreen = device.getFullScreenWindow() == frame;
        if (wasFullscreen) {
            // 독점 전체 화면을 해제하면 OS가 모드를 되돌릴 수 있으므로 먼저 원래 모드로 복구합니다.
            restoreDisplayMode();
            device.setFullScreenWindow(null);
        } else {
            // OS가 전체 화면 해제 시 복원한 모드를 다시 적용하지 않도록 보관 상태만 비웁니다.
            clearDisplayModeState();
        }
        frame.dispose();
        installRestoreOnClose(frame);

        switch (mode) {
            case "창 모드":
                frame.setUndecorated(false);
                frame.setResizable(true);
                frame.setExtendedState(JFrame.NORMAL);
                frame.setSize(size[0], size[1]);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                break;
            case "전체 화면":
                frame.setUndecorated(true);
                frame.setResizable(false);
                device.setFullScreenWindow(frame);
                frame.setVisible(true);
                applyHardwareResolution(device, size[0], size[1]);
                break;
            case "테두리 없는 창":
                frame.setUndecorated(true);
                frame.setResizable(false);
                frame.setExtendedState(JFrame.NORMAL);
                frame.setSize(size[0], size[1]);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 화면 모드입니다: " + mode);
        }

        frame.revalidate();
        frame.repaint();
    }

    private static int[] parseResolution(String resolution) {
        if (resolution == null) return new int[]{1280, 720};
        String normalized = resolution.replace(" ", "").toLowerCase(java.util.Locale.ROOT);
        switch (normalized) {
            case "1600x900": return new int[]{1600, 900};
            case "1920x1080": return new int[]{1920, 1080};
            default: return new int[]{1280, 720};
        }
    }

    private void applyHardwareResolution(GraphicsDevice device, int width, int height) {
        if (!device.isDisplayChangeSupported()) {
            System.err.println("이 모니터에서는 전체 화면 해상도 변경을 지원하지 않습니다.");
            return;
        }

        DisplayMode current = device.getDisplayMode();
        DisplayMode match = findDisplayMode(device, current, width, height);
        if (match == null) {
            System.err.println("모니터가 요청한 해상도를 지원하지 않습니다: " + width + "x" + height);
            return;
        }

        try {
            changedDevice = device;
            originalMode = current;
            device.setDisplayMode(match);
        } catch (RuntimeException failure) {
            changedDevice = null;
            originalMode = null;
            System.err.println("전체 화면 해상도를 적용하지 못했습니다: " + failure.getMessage());
        }
    }

    /** 창 모드로 돌아가거나 앱을 닫을 때 하드웨어 해상도를 원래 값으로 복구합니다. */
    private void restoreDisplayMode() {
        if (changedDevice == null || originalMode == null) return;
        try {
            changedDevice.setDisplayMode(originalMode);
        } catch (RuntimeException failure) {
            System.err.println("모니터 해상도를 원래 값으로 복구하지 못했습니다: " + failure.getMessage());
        } finally {
            clearDisplayModeState();
        }
    }

    private void clearDisplayModeState() {
        changedDevice = null;
        originalMode = null;
    }

    private void installRestoreOnClose(JFrame frame) {
        if (restoreOnCloseInstalled) return;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                restoreDisplayMode();
            }
        });
        restoreOnCloseInstalled = true;
    }

    private static DisplayMode findDisplayMode(GraphicsDevice device, DisplayMode current,
                                                int width, int height) {
        DisplayMode fallback = null;
        for (DisplayMode candidate : device.getDisplayModes()) {
            if (candidate.getWidth() != width || candidate.getHeight() != height) continue;

            boolean bitDepthMatches = candidate.getBitDepth() == current.getBitDepth()
                    || candidate.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI
                    || current.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI;
            boolean refreshRateMatches = candidate.getRefreshRate() == current.getRefreshRate()
                    || candidate.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN
                    || current.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN;
            if (bitDepthMatches && refreshRateMatches) return candidate;
            if (fallback == null) fallback = candidate;
        }
        return fallback;
    }
}
