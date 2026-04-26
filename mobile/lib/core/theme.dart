import 'package:flutter/material.dart';

class AppColors {
  static const primary = Color(0xFF1E58D6);
  static const primaryDark = Color(0xFF1746B0);
  static const accentBlue = Color(0xFF3B82F6);
  static const accentGreen = Color(0xFF14B981);
  static const accentPurple = Color(0xFF8B5CF6);
  static const accentOrange = Color(0xFFF59E0B);
  static const accentTeal = Color(0xFF0EA5A5);
  static const accentPink = Color(0xFFEC4899);

  static const bgScaffold = Color(0xFFF5F7FB);
  static const surface = Colors.white;
  static const surfaceMuted = Color(0xFFF1F5FB);
  static const border = Color(0xFFE3E8F0);

  static const textPrimary = Color(0xFF111827);
  static const textSecondary = Color(0xFF6B7280);
  static const textMuted = Color(0xFF9CA3AF);
}

ThemeData buildAppTheme() {
  final scheme = ColorScheme.fromSeed(
    seedColor: AppColors.primary,
    brightness: Brightness.light,
    primary: AppColors.primary,
    surface: AppColors.surface,
  );

  return ThemeData(
    colorScheme: scheme,
    useMaterial3: true,
    scaffoldBackgroundColor: AppColors.bgScaffold,
    fontFamily: 'Roboto',
    textTheme: const TextTheme(
      headlineMedium: TextStyle(
        fontSize: 28,
        fontWeight: FontWeight.w800,
        color: AppColors.textPrimary,
        letterSpacing: -0.5,
      ),
      headlineSmall: TextStyle(
        fontSize: 24,
        fontWeight: FontWeight.w800,
        color: AppColors.textPrimary,
        letterSpacing: -0.3,
      ),
      titleLarge: TextStyle(
        fontSize: 18,
        fontWeight: FontWeight.w700,
        color: AppColors.textPrimary,
      ),
      titleMedium: TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.w600,
        color: AppColors.textPrimary,
      ),
      bodyMedium: TextStyle(fontSize: 14, color: AppColors.textPrimary),
      bodySmall: TextStyle(fontSize: 12, color: AppColors.textSecondary),
      labelLarge: TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.w600,
        color: AppColors.textPrimary,
      ),
    ),
    cardTheme: CardThemeData(
      elevation: 0,
      color: AppColors.surface,
      surfaceTintColor: Colors.transparent,
      shape: RoundedRectangleBorder(
        side: const BorderSide(color: AppColors.border, width: 1),
        borderRadius: BorderRadius.circular(18),
      ),
      margin: EdgeInsets.zero,
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: Colors.white,
      hintStyle: const TextStyle(color: AppColors.textMuted),
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.border),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.border),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.primary, width: 1.5),
      ),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        textStyle: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 20),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: AppColors.primary,
        side: const BorderSide(color: AppColors.border),
        textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
    ),
    chipTheme: ChipThemeData(
      backgroundColor: AppColors.surfaceMuted,
      selectedColor: AppColors.primary.withValues(alpha: 0.1),
      side: const BorderSide(color: AppColors.border),
      labelStyle: const TextStyle(
        fontSize: 13,
        fontWeight: FontWeight.w500,
        color: AppColors.textPrimary,
      ),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
    ),
    snackBarTheme: const SnackBarThemeData(
      behavior: SnackBarBehavior.floating,
      backgroundColor: Color(0xFF065F46),
      contentTextStyle: TextStyle(color: Colors.white),
    ),
    navigationBarTheme: NavigationBarThemeData(
      height: 68,
      indicatorColor: AppColors.primary.withValues(alpha: 0.12),
      backgroundColor: Colors.white,
      surfaceTintColor: Colors.transparent,
      labelTextStyle: const WidgetStatePropertyAll(
        TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w600,
          color: AppColors.textPrimary,
        ),
      ),
    ),
    dividerTheme: const DividerThemeData(
      color: AppColors.border,
      thickness: 1,
      space: 1,
    ),
  );
}
