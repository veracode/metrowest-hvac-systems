package com.metrowest.entity;

public enum Role
{
    ADMIN,
    MANAGER,
    TECHNICIAN,
    CUSTOMER,

    ;

    public String role_string()
    {
        return "ROLE_" + this.name();
    }

    /// Decodes a [String] representation of a user role into a [Role] value.
    ///
    /// @return the [Role] value that matches the provided `role_string`, or `null` if the value is invalid
    public static Role from_string(String role_string)
    {
        try
        {
            return Role.valueOf(role_string.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
