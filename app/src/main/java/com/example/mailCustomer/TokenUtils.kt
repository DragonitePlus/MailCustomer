import android.util.Base64
import org.json.JSONObject

object TokenUtils {

    /**
     * 解析JWT的Payload部分
     * @param token JWT Token
     * @return JSONObject 表示的Payload
     */
    fun decodeJwtPayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid JWT token format")
            }
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            JSONObject(payload)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从JWT中获取用户名
     * @param token JWT Token
     * @return String? 用户名
     */
    fun getUsernameFromToken(token: String): String? {
        val payload = decodeJwtPayload(token)
        return payload?.optString("sub") // Java端使用的是 "sub"
    }

    /**
     * 从JWT中解析是否为管理员
     * @param token JWT Token
     * @return Boolean 是否为管理员
     */
    fun isAdminFromToken(token: String): Boolean {
        val payload = decodeJwtPayload(token)
        return payload?.optBoolean("isAdmin", false) ?: false
    }
}

