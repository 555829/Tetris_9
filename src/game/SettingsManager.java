package game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class SettingsManager {
    //asset 폴더에있는 config.properties에서 설정값 저장된거 불러오고 저장하고 합니다
    private static final String FILE_PATH = "src/game/asset/config.properties";

    //==================================================================================================================
    //초기값 설정하는 곳 (config 파일같은거 없으면 이값으로 불러오고 파일생성해줍니다)
    //==================================================================================================================
    public int masterVolume = 80;
    public int bgmVolume = 60;
    public int fxVolume = 30;
    public String displayMode = "창 모드";
    public String resolution = "1280 x 720 ";
    public String p1Left = "LEFT", p1Right = "RIGHT", p1Soft = "DOWN", p1Hard = "SPACE" , p1Rotation = "V";
    public String p2Left = "A", p2Right = "D", p2Soft = "S", p2Hard = "W"  , p2Rotation = "L";

    //==================================================================================================================
    //설정한 값을 저장합니다
    //==================================================================================================================
    public void save() {
        Properties prop = new Properties();
        prop.setProperty("masterVolume", String.valueOf(masterVolume));
        prop.setProperty("bgmVolume", String.valueOf(bgmVolume));
        prop.setProperty("fxVolume", String.valueOf(fxVolume));
        prop.setProperty("displayMode", displayMode);
        prop.setProperty("resolution", resolution);

        prop.setProperty("p1Left", p1Left);
        prop.setProperty("p1Right", p1Right);
        prop.setProperty("p1Soft", p1Soft);
        prop.setProperty("p1Hard", p1Hard);
        prop.setProperty("p1Rotation", p1Rotation);

        prop.setProperty("p2Left", p2Left);
        prop.setProperty("p2Right", p2Right);
        prop.setProperty("p2Soft", p2Soft);
        prop.setProperty("p2Hard", p2Hard);
        prop.setProperty("p2Rotation", p2Rotation);

        try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
            prop.store(out, "TetrisGameSettingsConfig");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //==================================================================================================================
    //설정 파일(값) 불러옵니다
    //==================================================================================================================
    public void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            prop.load(in);
            masterVolume = Integer.parseInt(prop.getProperty("masterVolume", "80"));
            bgmVolume = Integer.parseInt(prop.getProperty("bgmVolume", "60"));
            fxVolume = Integer.parseInt(prop.getProperty("fxVolume", "30"));

            displayMode = prop.getProperty("displayMode", "창 모드");
            resolution = prop.getProperty("resolution", "1280 x 720");

            p1Left = prop.getProperty("p1Left", "LEFT");
            p1Right = prop.getProperty("p1Right", "RIGHT");
            p1Soft = prop.getProperty("p1Soft", "DOWN");
            p1Hard = prop.getProperty("p1Hard", "SPACE");
            p1Rotation = prop.getProperty("p1Rotation", "V");

            p2Left = prop.getProperty("p2Left", "A");
            p2Right = prop.getProperty("p2Right", "D");
            p2Soft = prop.getProperty("p2Soft", "S");
            p2Hard = prop.getProperty("p2Hard", "W");
            p2Rotation = prop.getProperty("p2Rotation", "L");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //==================================================================================================================
    //화면 모드 및 해상도 전환 (수정중임, 꼬여있음)
    //==================================================================================================================
    private DisplayMode findValidDisplayMode(GraphicsDevice gd, int width, int height) {
        DisplayMode currentMode = gd.getDisplayMode();
        DisplayMode bestMatch = null;

        for (DisplayMode mode : gd.getDisplayModes()) {
            // 가로, 세로 해상도가 일치하는지 확인
            if (mode.getWidth() == width && mode.getHeight() == height) {
                // [최우선] 현재 모니터의 주사율(Hz) 및 비트 깊이(32bit 등)와 완벽히 일치하는 모드
                if (mode.getBitDepth() == currentMode.getBitDepth() &&
                        mode.getRefreshRate() == currentMode.getRefreshRate()) {
                    return mode;
                }
                // [2순위] 비트 깊이만 일치하는 모드
                if (mode.getBitDepth() == currentMode.getBitDepth()) {
                    bestMatch = mode;
                } else if (bestMatch == null) {
                    bestMatch = mode;
                }
            }
        }
        return bestMatch; // 찾은 최적의 모드 반환 (없으면 null)
    }

    public void applyDisplaySettings(JFrame frame, String mode, String res) {
        this.displayMode = mode;
        this.resolution = res;

        int width = 1280, height = 720;
        if (res.contains("1600")) {
            width = 1600;
            height = 900;
        } else if (res.contains("1920")) {
            width = 1920;
            height = 1080;
        }

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        frame.dispose(); // 화면 변경을 위해 리소스 해제

        switch (mode) {
            case "창 모드" -> {
                gd.setFullScreenWindow(null);
                frame.setUndecorated(false);
                frame.setExtendedState(JFrame.NORMAL);
                frame.setSize(width, height);
                frame.setLocationRelativeTo(null);
            }
            case "전체 화면" -> {
                frame.setUndecorated(true);
                gd.setFullScreenWindow(frame); // 1. 먼저 전체 화면 모드로 전환

                // 2. 모니터 지원 목록에서 실제 존재하는 DisplayMode 탐색
                if (gd.isDisplayChangeSupported()) {
                    DisplayMode validMode = findValidDisplayMode(gd, width, height);

                    if (validMode != null) {
                        try {
                            gd.setDisplayMode(validMode); // 3. 검증된 디스플레이 모드 적용
                            System.out.println("해상도 변경 성공: " + validMode.getWidth() + "x" + validMode.getHeight());
                        } catch (Exception e) {
                            System.err.println("해상도 적용 실패: " + e.getMessage());
                        }
                    } else {
                        System.err.println("이 모니터는 " + width + "x" + height + " 해상도를 지원하지 않습니다.");
                    }
                }
            }
            case "테두리 없는 창" -> {
                gd.setFullScreenWindow(null);
                frame.setUndecorated(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        }

        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();
    }

}