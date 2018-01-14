package com.ming.tools.entrance;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class StaticServer extends Endpoint implements Serializable {

    private static final long serialVersionUID = -2935894641131954428L;
    /**
     * https://nginx.org/en/docs/http/ngx_http_core_module.html#location
     */
    @Parameter
    private String locationUri;
}
