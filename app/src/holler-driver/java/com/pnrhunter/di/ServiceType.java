package com.pnrhunter.di;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServiceType {
    @Expose
    @SerializedName("service_model")
    public String type;

    @Expose(serialize = false, deserialize = false)
    @SerializedName("name")
    public String name = "Sedan";
}
