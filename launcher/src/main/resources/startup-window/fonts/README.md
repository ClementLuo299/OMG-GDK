# Custom Font Setup

This directory is for custom fonts to be used in the startup window.

## Recommended Fonts

Here are some excellent free fonts that work great for a game development kit:

### 1. **Inter** (Highly Recommended)
- **Why**: Designed specifically for screens, excellent readability, very modern
- **Download**: https://fonts.google.com/specimen/Inter
- **Best for**: Professional, clean, modern look

### 2. **Poppins**
- **Why**: Friendly, geometric, versatile, great for UI
- **Download**: https://fonts.google.com/specimen/Poppins
- **Best for**: Friendly, approachable feel

### 3. **Montserrat**
- **Why**: Clean, professional, many weights available
- **Download**: https://fonts.google.com/specimen/Montserrat
- **Best for**: Professional, corporate look

### 4. **Manrope**
- **Why**: Modern, rounded, distinctive
- **Download**: https://fonts.google.com/specimen/Manrope
- **Best for**: Modern, unique look

### 5. **Space Grotesk**
- **Why**: Unique, modern, tech-friendly
- **Download**: https://fonts.google.com/specimen/Space+Grotesk
- **Best for**: Tech/developer aesthetic

## How to Add a Font

1. **Download the font** from Google Fonts (or another source)
   - Make sure to download the **Regular (400)** and **Bold (700)** weights

2. **Rename the files** to match this pattern:
   - Regular weight: `[FontName]-Regular.ttf`
   - Bold weight: `[FontName]-Bold.ttf`
   
   Examples:
   - `Inter-Regular.ttf` and `Inter-Bold.ttf`
   - `Poppins-Regular.ttf` and `Poppins-Bold.ttf`

3. **Place the files** in this directory:
   ```
   launcher/src/main/resources/startup-window/fonts/
   ```

4. **Update the font path** in `StartupWindowTheme.java`:
   - Change `CUSTOM_FONT_PATH` to match your font name
   - Example: `"/startup-window/fonts/Inter-Regular.ttf"`

5. **Rebuild and run** - the custom font will be automatically loaded!

## Notes

- Only the Regular and Bold weights are needed (the code will derive other styles)
- The font must be in `.ttf` (TrueType) format
- If the font file is not found, the system will automatically fall back to system fonts
- The font will be registered globally when loaded, so it can be used throughout the application

## Example: Using Inter Font

1. Download Inter from Google Fonts
2. Extract `Inter-Regular.ttf` and `Inter-Bold.ttf`
3. Rename and place them:
   - `Inter-Regular.ttf` → `launcher/src/main/resources/startup-window/fonts/Inter-Regular.ttf`
   - `Inter-Bold.ttf` → `launcher/src/main/resources/startup-window/fonts/Inter-Bold.ttf`
4. The code is already configured to look for `Inter-Regular.ttf` by default!

