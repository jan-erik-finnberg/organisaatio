UPDATE organisaatio SET oppilaitostyyppi=oppilaitostyyppi || '#1' WHERE NOT(oppilaitostyyppi is null);