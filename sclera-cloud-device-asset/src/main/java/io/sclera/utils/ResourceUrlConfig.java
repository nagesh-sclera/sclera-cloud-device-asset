package io.sclera.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResourceUrlConfig {

    @Value("${sclera.backup-path}")
    private String backup_path;

    @Value("${sclera.image-folder-path}")
    private String image_folder_path;

    @Value("${sclera.analytics-file-path}")
    private String analytics_file_path;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${sclera.mysql-container-name}")
    private String mysql_container_name;


    // new changes
    @Value("${sclera.server-checklist-images-url}")
    private String server_checklist_images_url;

    @Value("${sclera.server-checklist-images-absolute-path}")
    private String server_checklist_images_absolute_path;

    @Value("${sclera.global-qrcode-server-url}")
    private String global_qrcode_server_url;


    public String getBackup_path() {
        return backup_path;
    }

    public void setBackup_path(String backup_path) {
        this.backup_path = backup_path;
    }

    public String getImage_folder_path() {
        return image_folder_path;
    }

    public void setImage_folder_path(String image_folder_path) {
        this.image_folder_path = image_folder_path;
    }

    public String getAnalytics_file_path() {
        return analytics_file_path;
    }

    public void setAnalytics_file_path(String analytics_file_path) {
        this.analytics_file_path = analytics_file_path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMysql_container_name() {
        return mysql_container_name;
    }

    public void setMysql_container_name(String mysql_container_name) {
        this.mysql_container_name = mysql_container_name;
    }


    // new changes
    public String getServer_checklist_images_url() {
        return server_checklist_images_url;
    }

    public void setServer_checklist_images_url(String server_checklist_images_url) {
        this.server_checklist_images_url = server_checklist_images_url;
    }

    public String getServer_checklist_images_absolute_path() {
        return server_checklist_images_absolute_path;
    }

    public void setServer_checklist_images_absolute_path(String server_checklist_images_absolute_path) {
        this.server_checklist_images_absolute_path = server_checklist_images_absolute_path;
    }

    public String getGlobal_qrcode_server_url() {
        return global_qrcode_server_url;
    }

    public void setGlobal_qrcode_server_url(String global_qrcode_server_url) {
        this.global_qrcode_server_url = global_qrcode_server_url;
    }

}
