package com.quienlotiene.identidad.infrastructure.security;

import com.quienlotiene.identidad.domain.model.SecretoTotp;
import com.quienlotiene.identidad.domain.port.VerificadorCodigoTotp;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import java.time.Clock;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Verifica códigos TOTP con {@code dev.samstevens.totp} (DECISIONES-BACKEND, sección 11): SHA-1, 6 dígitos, pasos de
 * 30 segundos y tolerancia de un paso antes o después por desfase del reloj del teléfono. La hora sale del
 * {@link Clock} de la aplicación para poder probarlo.
 */
@Component
class SamstevensVerificadorCodigoTotp implements VerificadorCodigoTotp {

    private static final Pattern SEIS_DIGITOS = Pattern.compile("\\d{6}");
    private static final int PASOS_DE_TOLERANCIA = 1;

    private final CodeVerifier verificador;

    SamstevensVerificadorCodigoTotp(Clock reloj) {
        var verificadorConReloj = new DefaultCodeVerifier(
                new DefaultCodeGenerator(), () -> reloj.instant().getEpochSecond());
        verificadorConReloj.setAllowedTimePeriodDiscrepancy(PASOS_DE_TOLERANCIA);
        this.verificador = verificadorConReloj;
    }

    @Override
    public boolean esValido(SecretoTotp secreto, String codigo) {
        if (codigo == null || !SEIS_DIGITOS.matcher(codigo).matches()) {
            return false;
        }
        return verificador.isValidCode(secreto.valorBase32(), codigo);
    }
}
