# ──────────────────────────────────────────────────────────────────────────────
# CrashGuard — Consumer ProGuard / R8 Rules
# ──────────────────────────────────────────────────────────────────────────────
# These rules are applied automatically to any app that depends on crash-guard.
# They are required because CrashLogStorage uses Java Serialization
# (ObjectOutputStream / ObjectInputStream). R8/ProGuard renames classes by
# default, which changes serialVersionUID values and corrupts serialized files
# across builds; deserialization then throws InvalidClassException and all
# stored crash logs are lost.
# ──────────────────────────────────────────────────────────────────────────────

# Keep all Serializable domain models intact (class name + fields)
-keep class io.github.alimsrepo.crashguard.domain.model.** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep the public API surface of CrashGuard so library consumers using R8 full
# mode do not have it stripped
-keep public class io.github.alimsrepo.crashguard.CrashGuard { public *; }
-keep public class io.github.alimsrepo.crashguard.domain.config.** { public *; }
-keep public interface io.github.alimsrepo.crashguard.domain.config.** { *; }

