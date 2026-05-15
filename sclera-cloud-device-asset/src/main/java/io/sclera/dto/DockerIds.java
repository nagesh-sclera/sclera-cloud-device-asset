package io.sclera.models.compositeclass;

import java.io.Serializable;

import io.sclera.models.Vdms;

public class DockerIds implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private Vdms vdms;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Vdms getVdms() {
        return vdms;
    }
    public void setVdms(Vdms vdms) {
        this.vdms = vdms;
    }

    public DockerIds() {
        super();
        // TODO Auto-generated constructor stub
    }

    public DockerIds(String name, Vdms vdms) {
        super();
        this.name = name;
        this.vdms = vdms;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((vdms == null) ? 0 : vdms.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DockerIds other = (DockerIds) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (vdms == null) {
            if (other.vdms != null)
                return false;
        } else if (!vdms.equals(other.vdms))
            return false;
        return true;
    }






}
