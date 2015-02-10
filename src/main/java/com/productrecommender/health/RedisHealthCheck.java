package com.productrecommender.health;

import com.codahale.metrics.health.HealthCheck;
import org.joda.time.DateTime;
import redis.clients.jedis.Jedis;

public class RedisHealthCheck extends HealthCheck {

    public static final String REDIS_HEALTH = "Redis Health";

    @Override
    protected Result check() throws Exception {
        Jedis jedis = new Jedis("localhost");
        DateTime now = DateTime.now();
        jedis.set(REDIS_HEALTH, now.toString());

        if (jedis.get(REDIS_HEALTH).equals(now.toString())){
            return HealthCheck.Result.healthy();
        } else {
            return HealthCheck.Result.unhealthy("Something odd is going on with redis.");
        }
    }
}
