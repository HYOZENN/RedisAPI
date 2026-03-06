# redisapi

API Redis modulable multi-plateforme (pub/sub + cache), sans dépendance à une plateforme de jeu. Utilisable comme bibliothèque sur Velocity, Spigot, Fabric ou tout environnement Java.

## Dépendance (côté consommateur)

```gradle
implementation("io.hyozen:redisapi:1.0-SNAPSHOT")
```

(Publicr la JAR avec `publishToMavenLocal` ou un dépôt Maven.)

## Dépendances internes

- Jedis 5.x, SLF4J, Gson

## Utilisation

### Configuration et connexion

```java
RedisConfig config = RedisConfig.builder()
    .host("localhost")
    .port(6379)
    .password("")
    .subscribePatterns(List.of("messaging:*"))  // défaut: messaging:*
    .build();

RedisService service = new RedisServiceImpl();
service.connect(config);
```

### Pub/Sub

```java
// Enregistrer un handler (avant ou après connect)
service.registerChannelHandler((stringHandlers, jsonHandlers) -> {
    stringHandlers.put("messaging:chat", msg -> System.out.println("Received: " + msg));
    jsonHandlers.put("messaging:event", json -> { /* traiter JSON */ });
});

// Publier
service.publish("messaging:chat", "Hello");
```

### Cache modulable

```java
// Cache par défaut (sans préfixe)
RedisCache cache = service.getCache();
cache.set("key", "value");
cache.set("key2", "value2", 60L);  // TTL 60 secondes
Optional<String> v = cache.get("key");
cache.delete("key");

// Cache avec namespace (préfixe) et TTL par défaut
RedisCache sessionCache = service.createCache("messaging:session:", 300L);
sessionCache.set("user:123", json);
```

### Déconnexion

```java
service.disconnect();
```

## Cycle de vie

L'appelant (plugin ou mod) crée le `RedisService`, appelle `connect(config)` au démarrage et `disconnect()` à l'arrêt.

## Tests

- Tests unitaires : `./gradlew test` (exclut les tests d'intégration).
- Tests d'intégration (Docker requis) : `./gradlew test -Pintegration`
