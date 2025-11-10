package com.example.sagivproject.models;

public class FirebaseErrorsHelper {
    public static String getFriendlyFirebaseAuthError(Exception exception) {
        if (exception == null) return "הפעולה נכשלה. נסה שוב.";

        String message = exception.getMessage();

        // שגיאות הרשמה
        if (message.contains("email address is already in use")) {
            return "כתובת האימייל כבר רשומה במערכת.";
        } else if (message.contains("operation-not-allowed")) {
            return "הפעולה לא אפשרית. ייתכן שצריך להפעיל את שיטת ההתחברות בקונסול Firebase.";
        } else if (message.contains("PERMISSION_DENIED")) {
            return "אין הרשאה לגשת לנתונים";
        } else if (message.contains("UNAVAILABLE")) {
            return "שירות הנתונים אינו זמין כרגע";
        } else if (message.contains("NOT_FOUND")) {
            return "הנתונים לא נמצאו";
        }

        // שגיאות התחברות
        else if (message.contains("user-not-found") || message.contains("There is no user record corresponding to this identifier")) {
            return "אין משתמש עם כתובת אימייל זו.";
        } else if (message.contains("wrong-password") || message.contains("The supplied auth credential is incorrect")) {
            return "כתובת האימייל או הסיסמה שגויה.";
        } else if (message.contains("A network error")) {
            return "שגיאת רשת. בדוק את החיבור לאינטרנט ונסה שוב.";
        } else if (message.contains("too-many-requests")) {
            return "בוצעו יותר מדי ניסיונות. אנא נסה שוב מאוחר יותר.";
        } else if (message.contains("account-exists-with-different-credential")) {
            return "המשתמש כבר קיים עם אמצעי התחברות אחר.";
        } else if (message.contains("requires-recent-login")) {
            return "הפעולה דורשת התחברות מחדש.";
        }

        // ברירת מחדל
        return "שגיאה: " + message;
    }
}
