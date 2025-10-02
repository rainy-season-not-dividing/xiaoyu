-- 参数说明
-- KEYS[1] : 任务哈希键  yuji:task:{taskId}
-- KEYS[2] : 任务订单集合键  yuji:task:{taskId}:orders
-- ARGV[1] : 当前用户 id

local taskKey   = KEYS[1]
local orderKey  = KEYS[2]
local userId    = tonumber(ARGV[1])

-- 1. 任务是否存在
local exists = redis.call('EXISTS', taskKey)
if exists == 0 then
    return 1   -- 任务不存在
end

-- 2. 任务状态是否为 RECRUIT
local status = redis.call('HGET', taskKey, 'status')
if status ~= 'RECRUIT' then
    return 2   -- 任务状态异常，不处于招聘状态
end

-- 3. 是否已有订单（只要集合里有人就算已存在）
local orderExists = redis.call('EXISTS', orderKey)
if orderExists == 1 and redis.call('SCARD', orderKey) > 0 then
    return 3   -- 该任务已存在订单
end

-- 4. 不能接自己发布的任务
local publisherId = tonumber(redis.call('HGET', taskKey, 'publisherId'))
if publisherId == userId then
    return 4   -- 不能自己接自己发布的任务
end

-- 全部校验通过
return 0