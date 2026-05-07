package ru.rbpo.backend.binary;

/** Константы формата бинарного экспорта сигнатур (multipart.md). */
public final class SignatureBinaryFormat {

    public static final int MANIFEST_VERSION = 1;
    public static final int DATA_VERSION = 1;

    /** Полная база (только ACTUAL). */
    public static final int EXPORT_FULL = 0;
    /** Инкремент (ACTUAL и DELETED). */
    public static final int EXPORT_INCREMENT = 1;
    /** Выборка по списку идентификаторов. */
    public static final int EXPORT_BY_IDS = 2;

    /** Значение sinceEpochMillis для неинкрементальных выгрузок. */
    public static final long SINCE_NOT_APPLICABLE = -1L;

    /** Компактный код статуса в манифесте. */
    public static final byte STATUS_ACTUAL = 1;
    public static final byte STATUS_DELETED = 2;

    private SignatureBinaryFormat() {}
}
