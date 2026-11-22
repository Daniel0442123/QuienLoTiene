# NF-SEG-009 · Re-prueba en producción sobre v0.5.3

- Fecha y hora: 21/11/2026 23:55 (hora de Bogotá).
- Versión: `v0.5.3` (`ff9ac6d`), [despliegue 13](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44986293432) en verde.
- Cómo: 6 `POST https://quienlotiene.tech/api/v1/auth/login` seguidos desde el navegador. Se usó el correo `limite-nf-seg-009@prueba.quienlotiene.tech`, que no tiene cuenta, con una frase equivocada distinta en cada intento.

| Intento | HTTP | Retry-After | Cookie de sesión | Respuesta |
|---|---|---|---|---|
| 1 a 5 | 401 | — | No | «El correo o la frase de contraseña no son correctos.» |
| 6 | 429 | 59 | No | «Hiciste 5 intentos de inicio de sesión en un minuto. Espera un momento y vuelve a intentar.» (Demasiados intentos) |

Resultado: **Aprobado**. El sexto intento dentro del mismo minuto se rechaza con 429 y Retry-After, como pide RNF-009. DEF-013 puede pasar a «En verificación».
