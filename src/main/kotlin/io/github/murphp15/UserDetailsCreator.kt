package io.github.murphp15

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component


interface UserDetailsCreator {
    fun createFromToken(username: String, roles: List<String>): UserDetails?
}


@Component
class DefaultUserDetailsCreator : UserDetailsCreator {
    override fun createFromToken(username: String, roles: List<String>): UserDetails? =
        User(username, "ALL_READY_LOGGED_IN", roles.map(::SimpleGrantedAuthority))
}