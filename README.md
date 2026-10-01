# Craft Cost Comparator

Is it cheaper to buy something, or to make it yourself? This plugin answers
that in the sidebar, using live Grand Exchange prices and counting whatever
ingredients you already have in your inventory and bank.

Currently covers **109 recipes** across two skills. Herblore: standard potions,
combination potions like super combat and stamina, herb tars, and barbarian
mixes. Cooking: every fish and meat worth cooking, plus pies, cakes, pizzas,
stews, bread and wine. More skills are planned.

## What it does

Search for an item and it becomes a card in your watchlist. Each card shows:

- the Grand Exchange price of the finished item, for however many you want
- every ingredient, with its icon, how many you own versus how many you need,
  and what that line costs
- the total cost to craft versus the cost to just buy, and which one wins
- a details drawer (the small arrow by the name) with the XP, an estimated
  time, the skill level required, and the quest needed to train the skill —
  green with a strikethrough once you've completed it, red if you haven't
- for anything that can fail, a note saying when it stops failing — amber while
  it still applies to you, grey once you're past it

Set the quantity with the **Qty** spinner and everything scales — including
recipes that produce a batch, like herb tars, where 40 tars correctly costs
three makes rather than forty.

### Grand Exchange sync

Tick **Auto-show GE item** and whenever you open a buy offer for an item the
plugin knows, its card is added automatically, with the quantity and
price-per-item tracking what you type into the offer, live, before you confirm
it. Close the offer screen and the card goes back to the usual Grand Exchange
price, since the offer price isn't live any more.

The card shows whatever the **Price per item** box shows, from the moment the
offer screen opens. The minus and plus buttons, both pairs of percentage
buttons, any percentages you've configured yourself, and a typed-in price all
work, because the plugin reads the box rather than trying to follow the
buttons.

It reads the box rather than a game variable because there is no variable to
read — the price lives only in the offer screen until you confirm. The box is
found by its label rather than a fixed position, so a layout change moves both
together. If it ever can't be found, the quantity sync carries on and the cards
fall back to the guide price rather than showing a wrong number.

### Owned counts

RuneLite only knows your bank contents once you've opened your bank in the
current session. If owned counts look wrong for a banked item, open the bank
once and they'll populate.

### Burning

Cooking costs assume nothing burns, which is only true at or above the level
where the food stops burning. The details drawer carries that level for each
food, and highlights it until you're past it.

There is no single such level — it depends on whether you use a fire or a
range, whether you're wearing cooking gauntlets, and your Hosidius favour — so
each note states the condition it applies to. A few foods burn at every level
without a Cooking cape, and the note says that instead. Bass has no note at
all: the wiki's item page and its burn table disagree about it, and a number
that can't be settled isn't worth showing.

## Notes on accuracy

Item IDs come from RuneLite's own `ItemID` table and every one was checked to
be Grand Exchange tradeable, so the plugin never shows a recipe it can't price.
That means a few potions are deliberately absent: Relicym's balm and Sanfew
serum need snake weed, and the divine potions need crystal dust, none of which
can be bought on the Grand Exchange. Levels, XP and ingredient quantities were
checked against the OSRS Wiki.

The estimated time is an approximation based on community timing guides rather
than a published game constant, so treat it as a ballpark.

Barbarian mixes also require Barbarian Training with Otto Godblessed, which
isn't a quest, so the details drawer lists only Druidic Ritual for them.

Multi-step Cooking recipes are listed by what you'd actually buy. A garden pie
really passes through three separate "part garden pie" items, but none of them
has a Grand Exchange price, so the shell, tomato, onion and cabbage are listed
instead. Where an intermediate is itself tradeable and is what you'd buy — a
pie shell, pizza base or bread dough — it's used directly. XP is for the step
a recipe's own ingredients pay for, so adding meat to a plain pizza shows 26,
not the 169 you'd get by also counting the baking of the base, which the plain
pizza's own card already accounts for.

## Building it yourself

Requires JDK 11 or newer and IntelliJ IDEA. Open the project, let Gradle sync,
then run `CraftCostPluginTest` — it launches a normal RuneLite client with the
plugin loaded. If you run it from IntelliJ's generated configuration rather
than the Gradle `run` task, add `-ea` to the VM options.

Logging in with a Jagex account requires the
[Jagex Accounts guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

## Adding recipes

Recipes live in one file per skill (`HerbloreRecipes.java`,
`CookingRecipes.java`), collected by `RecipeDatabase`. Each entry names its items through RuneLite's `ItemID`
constants rather than raw numbers, so a mistyped item is a compile error
instead of a silently wrong price.
