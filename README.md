# QuincuLandsWar
Re-implementing the lands war with kill points and capture flags

Requirements in wars.yml:
- Kill points must be set to zero. (They are handled separately by this plugin)
- BEACON and IRON_BLOCK must be in the break block list.

Hold time and placement cooldown will be synced to the values in the wars.yml 
- Set the times to their values in seconds for best results. eg. "150s"

You may change the capture flag recipe to anything you like. This plugin uses those crafted capture flags to place down caps.

This re-implementation is designed for Land vs. Land wars (not Nation vs. Nation).

The war team for each land is defined by:
- Its members
- All members of every land in its nation (if it has a nation)
- All players allied to the nation
- All players allied to the town at war

It is still best for each attacker and defender to join (get trusted into) their respective claims for full permissions. 

Kill points will count if you kill any person in the enemy war team, or if anyone in the enemy war team dies.
