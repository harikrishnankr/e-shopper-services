package com.eshopper.catalog.brand;

import com.eshopper.catalog.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(schema = "catalog", name = "brands")
public class Brand extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String logoKey;

    protected Brand() {}

    public Brand(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public void changeLogo(String logoKey) { this.logoKey = logoKey; }

    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getLogoKey() { return logoKey; }

    public void rename(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public void removeLogo() { this.logoKey = null; }
}
