# Guía para crear el repositorio del proyecto en GitHub

Sigue estos pasos para publicar el proyecto **Circulares** en GitHub.

---

## 1. Requisitos

- Una cuenta en https://github.com
- **Git** instalado: comprueba con `git --version`. Si no lo tienes, descárgalo de https://git-scm.com.
- (Opcional pero recomendado) **GitHub CLI** (`gh`): https://cli.github.com

---

## 2. Antes de subir: revisa la seguridad

El proyecto incluye un `.gitignore` que ya excluye `build/`, `.idea/`, `local.properties` y los keystores (`*.jks`, `*.keystore`). **Nunca subas claves de firma.**

Sobre `app/google-services.json`:
- Si tu repositorio será **privado**, puedes subirlo sin problema.
- Si será **público**, edita `.gitignore` y descomenta la línea `# app/google-services.json` para excluirlo. Aunque ese archivo no contiene secretos críticos, es buena práctica no exponer identificadores del proyecto en repos públicos.

---

## 3. Inicializar Git en el proyecto

Abre una terminal **dentro de la carpeta `CircularesApp`** y ejecuta:

```bash
cd CircularesApp
git init
git add .
git commit -m "Versión inicial: app de difusión de circulares (Kotlin + MVVM + Firebase)"
```

Configura tu identidad si es la primera vez que usas Git:
```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu_correo@ejemplo.com"
```

---

## 4. Crear el repositorio en GitHub

### Opción A — Desde la web (más sencilla)

1. Entra a https://github.com y pulsa el botón **+ → New repository**.
2. **Repository name:** `circulares-app` (o el que prefieras).
3. **Description:** "App Android de difusión de circulares e instructivos (Kotlin, MVVM, Firebase)".
4. Elige **Private** (recomendado) o **Public**.
5. **NO** marques "Add a README", ".gitignore" ni "license" (el proyecto ya los trae; evitas conflictos).
6. Pulsa **Create repository**.
7. GitHub te mostrará la URL del repo. Cópiala (ej. `https://github.com/tu_usuario/circulares-app.git`).

Conecta tu proyecto local y sube:
```bash
git remote add origin https://github.com/tu_usuario/circulares-app.git
git branch -M main
git push -u origin main
```

Si GitHub pide autenticación, usa un **Personal Access Token** como contraseña (Settings → Developer settings → Personal access tokens → Tokens (classic) → marca el scope `repo`).

### Opción B — Con GitHub CLI (un solo comando)

```bash
gh auth login          # solo la primera vez
gh repo create circulares-app --private --source=. --remote=origin --push
```
Esto crea el repo y sube el código de una vez.

---

## 5. Verificar

1. Recarga la página del repositorio en GitHub.
2. Deberías ver la estructura: `app/`, `docs/`, `build.gradle.kts`, `settings.gradle.kts`, etc.
3. Comprueba que **NO** aparecen `build/`, `.idea/` ni archivos `.jks`.

---

## 6. Flujo de trabajo para cambios futuros

```bash
# 1. Edita tu código en Android Studio
# 2. Revisa qué cambió
git status

# 3. Prepara y confirma
git add .
git commit -m "Descripción clara del cambio"

# 4. Sube a GitHub
git push
```

### Trabajar con ramas (recomendado para nuevas funciones)
```bash
git checkout -b nueva-funcionalidad
# ...trabajas y haces commits...
git push -u origin nueva-funcionalidad
# Luego abres un Pull Request en GitHub para revisarlo y fusionarlo a main
```

---

## 7. (Opcional) README destacado en GitHub

El proyecto incluye un `README.md` en la raíz que GitHub mostrará automáticamente en la portada del repositorio. Edítalo para añadir capturas de pantalla, créditos o instrucciones específicas de tu organización.

---

## 8. (Opcional) Compilación automática con GitHub Actions

Puedes hacer que GitHub compile el APK en cada push. Crea el archivo `.github/workflows/android.yml`:

```yaml
name: Android CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Construir APK de depuración
        run: ./gradlew assembleDebug
      - name: Publicar APK como artefacto
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

> Para que la compilación en Actions conecte con Firebase necesitarías inyectar `google-services.json` como *secret*. Para CI básico que solo verifica que compila, puedes usar un `google-services.json` de ejemplo.
