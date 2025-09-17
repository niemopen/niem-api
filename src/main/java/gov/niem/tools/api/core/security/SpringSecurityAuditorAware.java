package gov.niem.tools.api.core.security;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;

/**
 * Gets the user or system interacting with this application for auditing purposes.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    // SecurityContext context = SecurityContextHolder.getContext();
    // Authentication authentication = context.getAuthentication();
    // var niemUserPrincipal = (NiemUserPrincipal) authentication.getPrincipal();

    // return authentication != null ? Optional.of((String) niemUserPrincipal.getUsername())
    //     : Optional.of("0");

    return Optional.of("default");
  }

}
