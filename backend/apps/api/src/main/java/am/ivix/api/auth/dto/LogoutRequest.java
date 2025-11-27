package am.ivix.api.auth.dto;

/**
 * Запрос на logout: для корректного лог-аута
 * мы можем передавать оба токена.
 */
public class LogoutRequest {

    private String accessToken;
    private String refreshToken;

    public LogoutRequest() {
    }

    public LogoutRequest(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
