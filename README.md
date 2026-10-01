# Craft Cost Comparator

Is it cheaper to buy a potion, or to make it yourself? This plugin answers that
in the sidebar, using live Grand Exchange prices and counting whatever
ingredients you already have in your inventory and bank.

Currently covers **65 Herblore recipes** — standard potions, combination
potions like super combat and stamina, herb tars, and barbarian mixes. Other
skills are planned.

## What it does

Search for an item and it becomes a card in your watchlist. Each card shows:

- the Grand Exchange price of the finished item, for however many you want
- every ingredient, with its icon, how many you own versus how many you need,
  and what that line costs
- the total cost to craft versus the cost to just buy, and which one wins
- a details drawer (the small arrow by the name) with the XP, an estimated
  time, the skill level required, and the quest needed to train the skill —
  green with a strikethrough once you've completed it, red if you haven't

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

## Building it yourself

Requires JDK 11 or newer and IntelliJ IDEA. Open the project, let Gradle sync,
then run `CraftCostPluginTest` — it launches a normal RuneLite client with the
plugin loaded. If you run it from IntelliJ's generated configuration rather
than the Gradle `run` task, add `-ea` to the VM options.

Logging in with a Jagex account requires the
[Jagex Accounts guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

## Adding recipes

Recipes live in one file per skill (`HerbloreRecipes.java`), collected by
`RecipeDatabase`. Each entry names its items through RuneLite's `ItemID`
constants rather than raw numbers, so a mistyped item is a compile error
instead of a silently wrong price.
