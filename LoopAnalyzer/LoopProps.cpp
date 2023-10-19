#include "llvm/Analysis/LoopInfo.h"
#include "llvm/Analysis/LoopPass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Instruction.h"
#include "llvm/Pass.h"
#include "llvm/Support/raw_ostream.h"

// Rebuild after editing: build -> "make jtg2595-LoopAnalysis"

// Function: Iterates through each loop in program and analyzes properties of
// each loop

using namespace llvm;

namespace {

struct LoopProps : public LoopPass {
  static char ID;
  LoopProps() : LoopPass(ID) {}

  // Function: name of the function containing this loop.
  std::string getName(Loop *L) const {
    if (Function *F = L->getHeader()->getParent()) {
      return F->getName().str();
    }
    return "";
  }

  // Loop depth: 0 if it is not nested in any loop;
  // otherwise, it is 1 more than that of the loop in which it is nested in.
  int getDepth(Loop *L) const {
    if(L -> getParentLoop() == nullptr){
      return 0;
    }
    return 1 + getDepth(L -> getParentLoop());
  }

  // Contains nested loops: determine whether it has
  // any loop nested within it.
  std::string hasSubLoops(Loop *L) const {
    if (L->getSubLoops().empty()) {
      return "false";
    }
    return "true";
  }

  // Number of top-level basic blocks: count all basic
  // blocks in it but not in any of its nested loops.
  int numBlocks(Loop *L, LoopInfo &LI) const {
    int blocks = 0;
    for (BasicBlock *BB : L->getBlocks()) {
      if(LI.getLoopFor(BB) == L){
        blocks++;
      }
    }
    return blocks;
  }

  // Number of instructions: count all instructions in it,
  // including those in its nested loops.
  int numInstructions(Loop *L) const {
    int instrs = 0;
    for (BasicBlock *BB : L->getBlocks()) {
      for (Instruction &I : *BB) {
        instrs++;
      }
    }
    return instrs;
  }

  // Number of atomic operations: count atomic instructions in it,
  // including those in its nested loops.
  int numAtomics(Loop *L) const {
    int atomics = 0;
    for (BasicBlock *BB : L->getBlocks()) {
      for (Instruction &I : *BB) {
        if (I.isAtomic()) {
          atomics++;
        }
      }
    }
    return atomics;
  }

  // Number of top-level branch instructions: count branch
  // instructions in it but not in any of its nested loops.
  int numBranches(Loop *L, LoopInfo &LI) const {
    int branches = 0;
    for (BasicBlock *BB : L->getBlocks()) {
      if (LI.getLoopFor(BB) == L){
        for (Instruction &I : *BB) {
          if (auto *BI = dyn_cast<BranchInst>(&I)) {
            branches++;
          }
        }
      }
    }
    return branches;
  }

  bool runOnLoop(Loop *L, LPPassManager &LPM) override {
    LoopInfo &LI = getAnalysis<LoopInfoWrapperPass>().getLoopInfo();
    static int loopID = 0;
    std::string func = getName(L);
    int depth = getDepth(L);
    std::string subLoops = hasSubLoops(L);
    int BBs = numBlocks(L, LI);
    int instrs = numInstructions(L);
    int atomics = numAtomics(L);
    int branches = numBranches(L, LI);
    errs() << loopID << ": func=" << func << ", depth=" << depth
           << ", subLoops=" << subLoops << ", BBs=" << BBs
           << ", instrs=" << instrs << ", atomics=" << atomics
           << ", branches=" << branches << "\n";
    loopID++;
    return false;
  }

  // Program isnt modified, so all analysis is preserved
  void getAnalysisUsage(AnalysisUsage &AU) const override {
    AU.addRequired<LoopInfoWrapperPass>();
    AU.setPreservesAll();
  }

};

} // namespace

// Register pass as jtg2595-loop-props. X is a module level pass
char LoopProps::ID = 0;
static RegisterPass<LoopProps> X("jtg2595-loop-props",
                                 "My Loop Properties Pass");
