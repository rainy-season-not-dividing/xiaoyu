if (redis.call('get',KEYS[1])==ARVG[1]) then
    -- 释放锁 del key
    return redis.call('del',KEYS[1])
end

return 0