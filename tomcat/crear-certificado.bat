@echo off
rem Crea el certificado HTTPS (autofirmado, 1 año) para el host aquacontrol.local.
rem Requiere el JDK 17 (keytool debe estar en el PATH). Si usas otro nombre de host, cámbialo aquí y en app.properties.
keytool -genkeypair -alias aquacontrol -keyalg RSA -keysize 2048 -validity 365 ^
  -storetype PKCS12 -keystore aquacontrol.p12 -storepass changeit ^
  -dname "CN=aquacontrol.local" -ext "SAN=dns:aquacontrol.local"
keytool -exportcert -alias aquacontrol -keystore aquacontrol.p12 -storepass changeit -rfc -file aquacontrol.cer
echo.
echo Listo. Copia aquacontrol.p12 a la carpeta conf de Tomcat (ej. C:\tomcat11\conf).
echo Instala aquacontrol.cer como certificado de confianza si quieres que el navegador no avise.
pause
