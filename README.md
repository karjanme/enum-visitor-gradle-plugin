[license-image]: https://img.shields.io/github/license/karjanme/enum-visitor-gradle-plugin?label=License
[license-url]: https://github.com/karjanme/enum-visitor-gradle-plugin/blob/main/LICENSE

[gradle-plugin-portal-image]: https://img.shields.io/gradle-plugin-portal/v/me.karjan.enumvisitor?label=Gradle%20Plugin%20Portal
[gradle-plugin-portal-url]: https://plugins.gradle.org/plugin/me.karjan.enumvisitor

[ci-pipeline-image]: https://github.com/karjanme/enum-visitor-gradle-plugin/actions/workflows/ci.yml/badge.svg?branch=main
[ci-pipeline-url]: https://github.com/karjanme/enum-visitor-gradle-plugin/actions/workflows/ci.yml

[![license-image]][license-url]
[![gradle-plugin-portal-image]][gradle-plugin-portal-url]
[![ci-pipeline-image]][ci-pipeline-url]


# Enum Visitor Gradle Plugin

A Gradle plugin that automatically generates enums with an interface that implements the visitor pattern.


## Getting Started

Add the following to your `build.gradle` file:
```
plugins {
    id 'me.karjan.enumvisitor' version '0.1.0'
}
```

Define your enums under `src/main/enumvis/...`


## Example

**Example Input**

File: `src/main/enumvis/pkgA/pkg1/Planet.v`
```
Mercury
Venus
Earth
Mars
Jupiter
Saturn
Uranus
Neptune
```

**Example Output**

File: `build/generated-src/enumvis/pkgA/pkg1/Planet.java`
```
package pkgA.pkg1;

public enum Planet {
  Mercury {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitMercury();
    }
  },

  Venus {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitVenus();
    }
  },

  Earth {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitEarth();
    }
  },

  Mars {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitMars();
    }
  },

  Jupiter {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitJupiter();
    }
  },

  Saturn {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitSaturn();
    }
  },

  Uranus {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitUranus();
    }
  },

  Neptune {
    public <E> E accept(PlanetVisitor<E> visitor) {
      return visitor.visitNeptune();
    }
  };

  public abstract <E> E accept(PlanetVisitor<E> visitor);

  public interface PlanetVisitor<E> {
    E visitMercury();
    E visitVenus();
    E visitEarth();
    E visitMars();
    E visitJupiter();
    E visitSaturn();
    E visitUranus();
    E visitNeptune();
  }
}
```
