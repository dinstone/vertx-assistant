package com.dinstone.vertx.rs.model;

import com.dinstone.vertx.rs.util.Assert;

/**
 *
 */
public class Argument {

    /**
     * Query or Path type
     */
    private ParamType paramType;

    /**
     * parameter to search for in method annotations {@code @PathParam}
     * {@code @QueryParam}
     */
    private String paramName;

    /**
     * index matching method argument index 0..N-1
     */
    private int paramIndex = -1;

    /**
     * type of parameter expected by method
     */
    private Class<?> paramClazz;

    public Argument(ParamType paramType, String paramName, Class<?> paramClazz, int paramIndex) {
        Assert.notNull(paramName, "paramName is null");
        Assert.isTrue(paramIndex >= 0, "Can't set negative argument index!");

        this.paramName = paramName;
        this.paramType = paramType;
        this.paramIndex = paramIndex;
        this.paramClazz = paramClazz;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public Class<?> getParamClazz() {
        return paramClazz;
    }

    @Override
    public String toString() {
        return "Argument [paramIndex=" + paramIndex + ", paramName=" + paramName + ", paramType=" + paramType
                + ", paramClazz=" + paramClazz + "]";
    }

}
