package com.ming.tools.entrance;

import java.util.List;

/**
 * 入库
 *
 * @author CJ
 */
public interface Entrance {
    String getName();

    String getVersion();

    boolean isSsl();

    List<ApiServer> getApiServers();

    List<StaticServer> getStaticServers();
}
