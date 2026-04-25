package com.relatorio.transporte.service.sec;
import com.relatorio.transporte.entity.mysql.User;
import com.relatorio.transporte.repository.mysql.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtils {

    private final UserRepository userRepository;
    public SecurityUtils(UserRepository repo) {
        this.userRepository = repo;

    }

    public static UserPrincipal getCurrentPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    public User getCurrentUser() {
        var principal = getCurrentPrincipal();
        var usuario =  userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado no banco"));
        return usuario;
    }

    public static boolean isAdmin() {
        return getCurrentPrincipal().getRole().equals("ADMIN");
    }

    public static boolean isSupervisorOrAbove() {
        String role = getCurrentPrincipal().getRole();
        return role.equals("ADMIN") || role.equals("SUPERVISOR");
    }
}


//package com.helpdesk.shared.sec;
//
//import com.helpdesk.domain.user.entity.User;
//import com.helpdesk.domain.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class SecurityUtils {
//
//    private final UserRepository userRepository;
//
//    public UserPrincipal getCurrentPrincipal() {
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//         if(auth!=null){
//             System.out.println("usuario "+auth.getName());
//
//         }
//
//        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
//            throw new IllegalStateException("Usuário não autenticado");
//        }
//
//        return (UserPrincipal) auth.getPrincipal();
//    }
//
//    public User getCurrentUser() {
//        var principal = getCurrentPrincipal();
//
//        return userRepository.findById(principal.getId())
//                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado no banco"));
//    }
//
//    public boolean isAdmin() {
//        return getCurrentPrincipal().getRole().equals("ADMIN");
//    }
//
//    public boolean isSupervisorOrAbove() {
//        String role = getCurrentPrincipal().getRole();
//        return role.equals("ADMIN") || role.equals("SUPERVISOR");
//    }
//}