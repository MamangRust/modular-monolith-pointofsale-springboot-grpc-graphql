CREATE ALIAS IF NOT EXISTS MAKE_DATE AS $$
java.sql.Date makeDate(int year, int month, int day) {
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.clear();
    calendar.set(year, month - 1, day);
    return new java.sql.Date(calendar.getTimeInMillis());
}
$$;
