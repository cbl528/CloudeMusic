package com.singularity.cloudemusicadmin.common;

/**
 * 用户上下文，通过 ThreadLocal 存储当前登录用户信息。
 * <p>
 * 在 JwtAuthFilter 中设置，请求结束后自动清除，无需手动调用。
 * 请勿在任何异步线程中调用，ThreadLocal 不会跨线程传递。
 */
public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();

    public static void set(Long userId, String username) {
        userIdHolder.set(userId);
        usernameHolder.set(username);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static String getUsername() {
        return usernameHolder.get();
    }

    /** 清除当前线程的上下文。Filter 应在 finally 中调用此方法。 */
    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
    }
}
