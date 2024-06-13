package io.github.stuartwdouglas.domainproxy;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Property;

@RegisterForReflection(targets = { Bom.class, Component.class, Property.class })
public class ReflectionConfiguration {
}