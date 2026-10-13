package game;

import javax.swing.JFrame;
import java.io.*;
import java.util.Properties;

//======================================================================================================================
//설정 ui에서 받아온 값을 저장하거나 불러오는 등 설정과 관련된 일을 여기서 합니다
//======================================================================================================================
public class SettingsManager {
    //asset 폴더에있는 config.properties에서 설정값 저장된거 불러오고 저장하고 합니다
    private static final String FILE_PATH = "src/game/asset/config.properties";
    private final DisplaySettingsController displaySettings = new DisplaySettingsController();

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

    //ui에서 받아온 디스플레이 설정값을 디스플레이컨트롤러쪽에 전달해줍니다
    public void applyDisplaySettings(JFrame frame, String mode, String res) {
        this.displayMode = mode;
        this.resolution = res == null ? "1280 x 720" : res.trim();
        displaySettings.apply(frame, this.displayMode, this.resolution);
    }

}
