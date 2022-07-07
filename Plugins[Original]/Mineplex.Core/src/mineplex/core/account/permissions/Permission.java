package mineplex.core.account.permissions;

/**
 * A Permission that can be assigned to {@link PermissionGroup}s
 *
 * <p>
 * This interface is intended to be paired with enum data types, as only
 * enum-based permissions are accepted by {@link PermissionGroup#setPermission(Enum, boolean, boolean)}
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * enum ExamplePerm implements Permission { EXAMPLE_ONE, EXAMPLE_TWO }
 *
 * PermissionGroup.PLAYER.setPermission(ExamplePerm.EXAMPLE_ONE, true, true);
 * }
 * </pre>
 *
 * <p>
 * Permissions can either be inheritable (passed on to child groups) or group-specific. Group-specific permissions
 * override inherited permissions, but still allow the inherited permission to be propagated to children.
 *
 * <p>
 * In the case that two parents share the same inheritable permission, the values will be merged with logical OR
 */
public interface Permission {}
