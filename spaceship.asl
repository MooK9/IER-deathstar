// spaceship agent

free.

+free : base1(X,Y)     <- !prep_pos(X,Y).

+!prep_pos(X,Y) : free
  <- .print("prepping to got to ", X, ", ", Y);
     !pos(X,Y).

+!pos(X,Y) : pos(X,Y) & base1(X,Y) & base2(X2,Y2)
  <- .print("at base1, going to base2");
     !pos(X2,Y2).

+!pos(X,Y) : pos(X,Y) & base2(X,Y) & base3(X2,Y2)
  <- .print("at base2, going to base3");
     !pos(X2,Y2).

+!pos(X,Y) : pos(X,Y) & base3(X,Y) & base4(X2,Y2)
  <- .print("at base3, going to base4");
     !pos(X2,Y2).

+!pos(X,Y) : pos(X,Y) & base4(X,Y) & base1(X2,Y2)
  <- .print("at base4, going to base1");
     !pos(X2,Y2).

+!pos(X,Y) : pos(X,Y) <- .print("I've reached ",X,"x",Y).
+!pos(X,Y) : not pos(X,Y)
  <- .print("going to ", X, ", ", Y);
     !next_step(X,Y);
     !pos(X,Y).

+!next_step(X,Y)
   :  pos(AgX,AgY)
   <- jia.get_direction(AgX, AgY, X, Y, D);
      //.print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
      do(D).
+!next_step(X,Y) : not pos(_,_) // I still do not know my position
   <- !next_step(X,Y).
-!next_step(X,Y) : true  // failure handling -> start again!
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      !next_step(X,Y).

// someone else sent me an enemy location
+enemy(X1,Y1)[source(A)]
  :  A \== self &
     not allocated(enemy(X1,Y1),_) & // The enemy was not allocated yet
     not fighting &            // I am not fighting
     free &                         // and I am free
     pos(X2,Y2) &
     .my_name(Me)
  <- jia.dist(X1,Y1,X2,Y2,D);       // bid
     .print("Bidding for ",enemy(X1,Y1));
     .send(radar,tell,bid(enemy(X1,Y1),D,Me)).

// bid high as I'm not free
+enemy(X1,Y1)[source(A)]
  :  A \== self & .my_name(Me)
  <- .print("Bidding for ",enemy(X1,Y1), " with 10000");
     .send(radar,tell,bid(enemy(X1,Y1),10000,Me)).

// enemy allocated to me
@palloc1[atomic]
+allocated(Enemy,Ag)[source(radar)]
  :  .my_name(Ag) & free // I am still free
  <- -free;
     .print("Enemy ",Enemy," allocated to ",Ag);
     !init_handle(Enemy).

// some enemy was allocated to me, but I can not
// handle it anymore, re-announce
@palloc2[atomic]
+allocated(Enemy,Ag)[source(radar)]
  :  .my_name(Ag) & not free // I am no longer free
  <- .print("I can not handle ",Enemy," anymore!");
     .print("(Re)announcing ",Enemy," to others");
     .broadcast(tell,Enemy).

// someone else destroyed the enemy I am going to go,
// so drops the intention and chose another enemy
@ppgd[atomic]
+destroyed(G)[source(A)]
  :  .desire(handle(G)) | .desire(init_handle(G))
  <- .print(A," has taken ",G," that I am pursuing! Dropping my intention.");
     .abolish(G);
     .drop_desire(handle(G));
     -enemy(X,Y)[source(_)];
     -allocated(enemy(X,Y),_)[source(_)];
     -destroyed(enemy(X,Y))[source(_)];
     -committed_to(enemy(X,Y))[source(_)];
     !!choose_enemy.

// someone else destroyed an enemy I know about,
// remove from my belief base
+destroyed(enemy(X,Y))
  <- -enemy(X,Y)[source(_)];
     -allocated(enemy(X,Y),_)[source(_)];
     -destroyed(enemy(X,Y))[source(_)];
     -committed_to(enemy(X,Y))[source(_)].

@pih1[atomic]
+!init_handle(Enemy)
  :  .desire(pos(_,_))
  <- .print("Dropping pos(_,_) desires and intentions to handle ",Enemy);
     .drop_desire(pos(_,_));
     !init_handle(Enemy).
@pih2[atomic]
+!init_handle(Enemy)
  :  pos(X,Y)
  <- .print("Going for ",Enemy);
     !!handle(Enemy). // must use !! to perform "handle" as not atomic

+!handle(enemy(X,Y))
  :  not free
  <- .print("Handling ",enemy(X,Y)," now.");
     .broadcast(tell, committed_to(enemy(X,Y)));
     !pos(X,Y);
     !ensure(destroy,enemy(X,Y));
     // broadcast that I got the enemy(X,Y), to avoid someone
     // else to pursue this enemy
     .broadcast(tell,destroyed(enemy(X,Y)));
     -enemy(X,Y)[source(_)];
     .print("Finish handling ",enemy(X,Y));
     !!choose_enemy.

// if ensure(destroy) failed, pursue another enemy
-!handle(G) : G
  <- .print("failed to catch enemy ",G);
     .abolish(G); // ignore source
     !!choose_enemy.
-!handle(G) : true
  <- .print("failed to handle ",G,", it isn't in the BB anyway");
     !!choose_enemy.

// no known enemy to choose from
// become free again to search for enemy
+!choose_enemy
  :  not enemy(_,_)
  <- -+free.

// Finished one enemy, but others left
// find the closest enemy among the known options,
// that nobody else committed to
+!choose_enemy
  :  enemy(_,_)
  <- .findall(enemy(X,Y),enemy(X,Y),LG);
     !calc_enemy_distance(LG,LD);
     .length(LD,LLD); LLD > 0;
     .print("Uncommitted enemy distances: ",LD,LLD);
     .min(LD,d(_,NewG));
     .print("Next enemy is ",NewG);
     !!handle(NewG).
-!choose_enemy <- -+free.

+!calc_enemy_distance([],[]).
+!calc_enemy_distance([enemy(GX,GY)|R],[d(D,enemy(GX,GY))|RD])
  :  pos(IX,IY) & not committed_to(enemy(GX,GY))
  <- jia.dist(IX,IY,GX,GY,D);
     !calc_enemy_distance(R,RD).
+!calc_enemy_distance([_|R],RD)
  <- !calc_enemy_distance(R,RD).

+!ensure(destroy,_) : pos(X,Y) & cell(X,Y,enemy)
  <- do(fight);
     !ensure(destroy,enemy(X,Y)).

+!ensure(destroy,_) : pos(X,Y) & not cell(X,Y,enemy)
  <- do(destroy).
     
/* end of a simulation */

+end_of_simulation(S,_) : true
  <- .drop_all_desires;
     .abolish(quadrant(_,_,_,_));
     .abolish(enemy(_,_));
     .abolish(committed_to(_));
     .abolish(destroyed(_));
     //.abolish(last_checked(_,_));
     -+free;
     .print("-- END ",S," --").

