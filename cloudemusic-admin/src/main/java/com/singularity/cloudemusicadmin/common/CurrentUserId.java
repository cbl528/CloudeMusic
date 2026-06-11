package com.singularity.cloudemusicadmin.common;

import java.lang.annotation.*;

/**
 * 标注在 Controller 方法参数上，自动注入当前登录用户的 ID。
 * <p>
 * 使用方式： {@code public Result<?> foo(@CurrentUserId Long userId)}
 * <p>
 * 需要 {@link CurrentUserIdArgumentResolver} 配合生效。
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId {
}
