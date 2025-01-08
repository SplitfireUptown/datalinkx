package com.datalinkx.dataserver.bean.vo;

import com.datalinkx.common.constants.MetaConstants.CommonConstant;
import org.apache.commons.lang3.StringUtils;

/**
 * 路由显示信息
 *
 * @author ruoyi
 */
public class MetaVo {
    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则会被 <keep-alive>缓存
     */
    private boolean isCache;

    /**
     * 内链地址（http(s)://开头）
     */
    private String link;

    public MetaVo() {
    }

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVo(String title, String icon, boolean isCache) {
        this.title = title;
        this.icon = icon;
        this.isCache = isCache;
    }

    public MetaVo(String title, String icon, String link) {
        this.title = title;
        this.icon = icon;
        this.link = link;
    }

    public MetaVo(String title, String icon, boolean isCache, String link) {
        this.title = title;
        this.icon = icon;
        this.isCache = isCache;
        if (StringUtils.startsWithAny(link, CommonConstant.HTTP, CommonConstant.HTTPS)) {
            this.link = link;
        }
    }

    public boolean isCache() {
        return isCache;
    }

    public void setIsCache(boolean isCache) {
        this.isCache = isCache;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
