package kindl.global.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kindl.global.auth.security.JwtAuthenticationEntryPoint.Companion.EXCEPTION_KEY
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)
        if (!token.isNullOrBlank()) {
            try {
                SecurityContextHolder.getContext().authentication = jwtProvider.getAuthentication(token)
            } catch (e: Exception) {
                // 인증 실패는 EntryPoint 에서 일관된 응답으로 변환한다.
                SecurityContextHolder.clearContext()
                request.setAttribute(EXCEPTION_KEY, e)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return if (header.startsWith(PREFIX)) {
            header.substring(PREFIX.length).trim().takeIf { it.isNotBlank() }
        } else {
            null
        }
    }

    companion object {
        private const val PREFIX = "Bearer "
    }
}
