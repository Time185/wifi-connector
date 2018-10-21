package io.github.weechang.generetor;

import io.github.weechang.config.FileConfig;
import io.github.weechang.config.Generetor;
import io.github.weechang.config.Profile;
import io.github.weechang.util.FileUtils;
import sun.nio.ch.DirectBuffer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配置文件生成器
 */
public class ProfileGeneretor {

    private String ssid = null;
    private Generetor generetor = Generetor.MOST_USE;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public ProfileGeneretor(String ssid, Generetor generetor) {
        this.ssid = ssid;
        this.generetor = generetor;
    }

    /**
     * 生成配置文件
     */
    public void genProfile() {
        List<String> passwordList = null;
        int counter = 0;
        outer:
        while (true) {
            int start = counter * FileConfig.PASSWORD_SIZE;
            int end = (counter + 1) * FileConfig.PASSWORD_SIZE - 1;
            passwordList = PasswordGeneretor.getPassword(generetor, start, end);
            if (passwordList != null && passwordList.size() > 0) {
                // 生成配置文件
                for (String password : passwordList) {
                    GenThread genThread = new GenThread(ssid, password);
                    threadPool.execute(genThread);
                }
            } else {
                break outer;
            }
            counter++;
        }
    }
}

class GenThread implements Runnable {

    private String ssid = null;
    private String password = null;

    GenThread(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }

    private String getProfilePath() {
        return FileConfig.PROFILE_PATH + "\\" + password + ".xml";
    }

    public void run() {
        String profileContent = Profile.PROFILE.replace(Profile.WIFI_NAME, ssid);
        profileContent = profileContent.replace(Profile.WIFI_PASSWORD, password);
        FileUtils.writeToFile(getProfilePath(), profileContent);
    }
}
