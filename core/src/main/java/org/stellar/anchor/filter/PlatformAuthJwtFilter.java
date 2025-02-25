package org.stellar.anchor.filter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.stellar.anchor.api.exception.SepValidationException;
import org.stellar.anchor.auth.ApiAuthJwt.PlatformAuthJwt;
import org.stellar.anchor.auth.JwtService;

public class PlatformAuthJwtFilter extends AbstractJwtFilter {
  public PlatformAuthJwtFilter(JwtService jwtService) {
    super(jwtService);
  }

  @Override
  public void check(String jwtCipher, HttpServletRequest request, ServletResponse servletResponse)
      throws Exception {
    @NonNull PlatformAuthJwt token = jwtService.decode(jwtCipher, PlatformAuthJwt.class);
    if (token == null) {
      throw new SepValidationException("JwtToken should not be null");
    }

    request.setAttribute(JWT_TOKEN, token);
  }
}
