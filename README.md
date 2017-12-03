# Haskell-Frege Interpreter for Apache Zeppelin

## Install

1. Unzip archive to `interpreter` folder in `zeppelin` directory
2. Open Zeppelin UI and go to `Interpreter` page
3. Click `Create` button and add new interpreter with name `haskell` and group `frege`

## Examples

### Simple expression

```
%haskell
3 * 9
--
> 27
```

### Lazy expression

```
%haskell
print [x*x | x <- [1..10]]
---
> [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]
```

### Multi-line expression

Normally we cannot mix definitions and method call in the same paragraph but there is one trick.
If there is `z_display` function defined in paragraph then interpreter will call that function when all definitions are compiled.
The `z_display` method will apply `show` function to display the result.

```
%haskell

import Data.List

fib :: Int -> Int
fib n = xs !! n
    where xs = 1 : 1 : zipWith (+) xs (tail xs)

z_display = fib 10
---
> 89
```

### Program

At the moment interpreter doesn't support normal main method and you need to use `z_main` instead.

```
%haskell
z_main :: IO ()
z_main = do
  print "hello world"
  println "!!!"
---
> hello world!!!
```
