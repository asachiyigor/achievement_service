ALTER TABLE achievement
    ALTER COLUMN rarity TYPE VARCHAR(50)
        USING CAST(rarity AS VARCHAR);

UPDATE achievement
SET rarity = CASE
                 WHEN rarity = '0' THEN 'COMMON'
                 WHEN rarity = '1' THEN 'UNCOMMON'
                 WHEN rarity = '2' THEN 'RARE'
                 WHEN rarity = '3' THEN 'EPIC'
                 WHEN rarity = '4' THEN 'LEGENDARY'
    END;