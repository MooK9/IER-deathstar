// radar agent

/* negotiation for enemy */

/*
+bid(Enemy,D,Ag)
  :  .count(bid(Enemy,_,_),3)  // three bids were received
  <- .print("bid from ",Ag," for ",Enemy," is ",D);
     !allocate_spaceship(Enemy);
     .abolish(bid(Enemy,_,_)).
+bid(Enemy,D,Ag)
  <- .print("bid from ",Ag," for ",Enemy," is ",D).

+!allocate_spaceship(Enemy)
  <- .findall(op(Dist,A),bid(Enemy,Dist,A),LD);
     .min(LD,op(DistCloser,Closer));
     DistCloser < 10000;
     .print("Enemy ",Enemy," was allocated to ",Closer, " options were ",LD);
     .broadcast(tell,allocated(Enemy,Closer)).
     //-Enemy[source(_)].
-!allocate_spaceship(Enemy)
  <- .print("could not allocate enemy ",Enemy).
*/

/* end of simulation plans */

@end[atomic]
+end_of_simulation(S,_) : true
  <- .print("-- END ",S," --");
     .abolish(init_pos(S,_,_)).

