package io.github.murphp15

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthorizationFilter(
    authenticationManager: AuthenticationManager,
    private val userDetailsCreator: UserDetailsCreator,
    private val jwtSecret: String
) : BasicAuthenticationFilter(authenticationManager) {

    private val log = LoggerFactory.getLogger(JwtAuthorizationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = getAuthentication(request)
        if (authentication == null) {
            filterChain.doFilter(request, response)
            return
        }

        SecurityContextHolder.getContext().authentication = authentication;
        filterChain.doFilter(request, response)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val header = request.getHeader("Authorization") ?: ""

        val token = request.queryString?.let { Regex("token=(.*?)$").find(it)?.groupValues?.get(1) }
            ?: if (header.startsWith(TOKEN_PREFIX)) {
                header.replace("Bearer ", "")
            } else {
                header
            }

        return parseToken(token)
    }

    private fun parseToken(token: String): UsernamePasswordAuthenticationToken? {
        if (token.isNotEmpty()) {
            try {
                val signingKey = jwtSecret.toByteArray()


                val parsedToken = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)


                val username = parsedToken.body.subject

                val rolesAsString = (parsedToken.body["rol"] as List<*>).filterIsInstance<String>()
                val authorities = rolesAsString
                    .map { authority -> SimpleGrantedAuthority(authority) }

                require(authorities.isNotEmpty()) { "user must have some roles" }
                if (username.isNotEmpty()) {
                    return UsernamePasswordAuthenticationToken(
                        userDetailsCreator.createFromToken(
                            username,
                            rolesAsString
                        )
                            ?: username, null,
                        authorities.subList(0, authorities.size - 1)
                    )
                }
            } catch (exception: ExpiredJwtException) {
                log.warn("Request to parse expired JWT : {} failed : {}", token, exception.message)
            } catch (exception: UnsupportedJwtException) {
                log.warn("Request to parse unsupported JWT : {} failed : {}", token, exception.message)
            } catch (exception: MalformedJwtException) {
                log.warn("Request to parse invalid JWT : {} failed : {}", token, exception.message)
            } catch (exception: SecurityException) {
                log.warn("Request to parse JWT with invalid signature : {} failed : {}", token, exception.message)
            } catch (exception: Exception) {
                log.warn("Request to parse empty or null JWT : {} failed : {}", token, exception.message)
            }
        }
        return null
    }

    private fun allowForRefreshToken(ex: ExpiredJwtException, request: HttpServletRequest) {

        // create a UsernamePasswordAuthenticationToken with null values.
        val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(null, null, null);
        // After setting the Authentication in the context, we specify
        // that the current user is authenticated. So it passes the
        // Spring Security Configurations successfully.
        SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken;
        // Set the claims so that in controller we will be using it to create
        // new JWT
        request.setAttribute("claims", ex.claims);

    }
}