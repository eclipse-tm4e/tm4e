-- Comments
-- Single-line comment

--[[
Multi-line
comment
]]

-- Variables
local number = 42
local string_var = "Hello, Lua!"
local boolean_var = true

-- Table (Lua's only compound data structure)
local my_table = {1, 2, 3, key = "value"}

-- Conditional statements
if boolean_var then
  print("It's true!")
else
  print("It's false!")
end

-- Loops
for i = 1, 3 do
  print("Iteration", i)
end

-- Functions
function greet(name)
  print("Hello, " .. name .. "!")
end

-- Function with multiple returns
function multiple_returns()
  return 1, "two", true
end

-- Calling functions
greet("Alice")

local a, b, c = multiple_returns()
print(a, b, c)

-- Anonymous functions (closures)
local square = function(x)
  return x * x
end

print("Square:", square(5))

-- Coroutines (simple example)
local co = coroutine.create(function()
  print("Coroutine started")
  coroutine.yield("Hello from coroutine")
  print("Coroutine ended")
end)

print(coroutine.resume(co))

-- Metatables and metamethods
local mt = {
  __add = function(a, b)
    return a + b
  end,
  __index = function(t, k)
    return "Key not found: " .. k
  end
}

local my_object = setmetatable({value = 10}, mt)

print(my_object + 5)
print(my_object.nonexistent_key)

-- Module system
local my_module = {}

function my_module.say_hello()
  print("Hello from the module!")
end

return my_module
