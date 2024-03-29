package cam


import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.util.log2Ceil
import scala.util.Random

class CAMModelTester extends AnyFlatSpec with ChiselScalatestTester {

    def writeIPIntoMemory(dut: FIFOCAMModel, IP: Int, IPIdx: Int): Unit = {
        require(IP >= 0 && IPIdx >= -1, "IP should >= 0 and IPIdx should >= -1.")
        dut.io.in.valid.poke(true.B)
        dut.io.in.bits.loadData.poke(IP.U)
        dut.io.in.ready.expect(true.B)
        dut.io.in.bits.opCode.poke(0.U)

        dut.clock.step()
        dut.io.in.valid.poke(false.B)
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.writtenIndex.valid.expect(true.B)
        if (IPIdx >= 0) {
          dut.io.writtenIndex.bits.expect(IPIdx.U)
        }
        

        dut.clock.step()
        dut.io.writtenIndex.valid.expect(false.B)
        dut.io.in.ready.expect(true.B)
    }

    def lookupIPInMemory(dut: FIFOCAMModel, IP: Int, IPIdx: Int): Unit = {
      require(IP >= 0 && IPIdx >= 0, "IP and IPIdx should be greater or equal to 0.")
        dut.io.in.valid.poke(true.B)
        dut.io.in.ready.expect(true.B)
        dut.io.in.bits.loadData.poke(IP.U)
        dut.io.in.bits.opCode.poke(1.U)

        dut.clock.step()
        dut.io.in.valid.poke(false.B)
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.lookupFound.bits.expect(true.B)
        dut.io.lookupFound.valid.expect(true.B)
        dut.io.lookupResult.valid.expect(true.B)
        dut.io.lookupResult.bits.expect(IPIdx.U)
    }


    def CAMModelTestWrite(dut: FIFOCAMModel, IPsVec: Seq[Int]): Unit = {
      
      IPsVec.zip(IPsVec.indices).foreach { case (ip, idx) =>
          writeIPIntoMemory(dut, ip, idx)
      }
    }

    def CAMModelTestWrite(dut: FIFOCAMModel, IP: Int): Unit = {
      
      writeIPIntoMemory(dut, IP, -1)
    }

    def CAMModelTestLookUp(dut: FIFOCAMModel, IPsVec: Seq[Int]): Unit = {
      
      IPsVec.zip(IPsVec.indices).foreach { case (ip, idx) =>
          writeIPIntoMemory(dut, ip, idx)
      }
      IPsVec.zip(IPsVec.indices).foreach { case (ip, idx) =>
          lookupIPInMemory(dut, ip, idx)
      }
    }

    def CAMModelTestLookUp(dut: FIFOCAMModel, lookupIP: Int, lookupIpIdx: Int): Unit = {
      lookupIPInMemory(dut, lookupIP, lookupIpIdx)
    }

    

    behavior of "IP Adding"
    it should "Add four IPs into memory " in {
      val p = CAMParams_FSM(128, 32)
      val randomValues = Seq.fill(4)(Random.nextInt(Int.MaxValue))
      test(new FIFOCAMModel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        CAMModelTestWrite(dut, randomValues)
      }
    }

    behavior of "IP Adding and lookup"
    it should "Add four IPs into memory " +
      "and look up added four IPs" in {
      val p = CAMParams_FSM(128, 32)
      val randomValues = Seq.fill(4)(Random.nextInt(Int.MaxValue))
      test(new FIFOCAMModel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        CAMModelTestWrite(dut, randomValues)
        CAMModelTestLookUp(dut, randomValues)
      }
    }

    behavior of "IP Adding and single lookup"
    it should "Add four IPs into memory " +
      "and look up the 1st added IP with correct index" in {
      val p = CAMParams_FSM(128, 32)
      val randomValues = Seq.fill(4)(Random.nextInt(Int.MaxValue))
      test(new FIFOCAMModel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        CAMModelTestWrite(dut, randomValues)
        CAMModelTestLookUp(dut, randomValues(1), 1)
      }
    }

    behavior of "IP Adding and Overriding"
    it should "Add five IPs into memory " +
      "and look up the last added IP with correct index" in {
      val p = CAMParams_FSM(128, 32)
      val randomValues = Seq.fill(4)(Random.nextInt(Int.MaxValue))
      val addRandomValue = Random.nextInt(Int.MaxValue)
      test(new FIFOCAMModel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        CAMModelTestWrite(dut, randomValues)
        CAMModelTestWrite(dut, addRandomValue)
        CAMModelTestLookUp(dut, addRandomValue, 0)
      }
    }


}