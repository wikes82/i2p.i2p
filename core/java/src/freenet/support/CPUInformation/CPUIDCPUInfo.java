package freenet.support.CPUInformation;

/**
 *  Moved out of CPUID.java
 *  Ref: http://en.wikipedia.org/wiki/CPUID
 *  @since 0.8.7
 */
abstract class CPUIDCPUInfo implements CPUInfo
{
    public String getVendor()
    {
        return CPUID.getCPUVendorID();
    }

    public boolean hasMMX()
    {
        return (CPUID.getEDXCPUFlags() & (1 << 23)) != 0; //EDX Bit 23
    }

    public boolean hasSSE(){
        return (CPUID.getEDXCPUFlags() & (1 << 25)) != 0; //EDX Bit 25
    }

    public boolean hasSSE2()
    {
        return (CPUID.getEDXCPUFlags() & (1 << 26)) != 0; //EDX Bit 26
    }

    public boolean hasSSE3()
    {
        return (CPUID.getECXCPUFlags() & (1 << 0)) != 0; //ECX Bit 0
    }

    public boolean hasSSE41()
    {
        return (CPUID.getECXCPUFlags() & (1 << 19)) != 0; //ECX Bit 19
    }

    public boolean hasSSE42()
    {
        return (CPUID.getECXCPUFlags() & (1 << 20)) != 0; //ECX Bit 20
    }

    public boolean hasSSE4A()
    {
        return (CPUID.getExtendedECXCPUFlags() & (1 << 6)) != 0; //Extended ECX Bit 6
    }
    
    /**
     * @return true iff the CPU supports the AVX instruction set.
     * @since 0.9.21
     */
    public boolean hasAVX()
    {
        return (CPUID.getECXCPUFlags() & (1 << 28)) != 0 && //AVX: ECX Bit 28
               (CPUID.getECXCPUFlags() & (1 << 27)) != 0;   //XSAVE enabled by OS: ECX Bit 27
    }

    /**
     * @return true iff the CPU supports the AVX2 instruction set.
     * @since 0.9.21
     */
    public boolean hasAVX2()
    {
        return hasAVX() &&
               (CPUID.getExtendedEBXCPUFlags() & (1 << 5)) != 0; //Extended EBX Bit 5
    }
    
    /**
     * @return true iff the CPU supports the AVX512 instruction set.
     * @since 0.9.21
     */
    public boolean hasAVX512()
    {
        return hasAVX() &&
               (CPUID.getExtendedEBXCPUFlags() & (1 << 5)) != 0; //Extended EBX Bit 5
    }
    
    /**
     * @return true iff the CPU supports the ADX instruction set.
     * @since 0.9.21
     */
    public boolean hasADX()
    {
        return hasAVX() &&
               (CPUID.getExtendedEBXCPUFlags() & (1 << 19)) != 0; //Extended EBX Bit 19
    }
    
    /**
     * @return true iff the CPU supports TBM.
     * @since 0.9.21
     */
    public boolean hasTBM()
    {
        return (CPUID.getECXCPUFlags() & (1 << 21)) != 0; //ECX Bit 21
    }
    
    /**
     * @return true iff the CPU supports the AES-NI instruction set.
     * @since 0.9.14
     */
    public boolean hasAES() {
        return (CPUID.getECXCPUFlags() & (1 << 25)) != 0; //ECX Bit 25
    }
    
    /**
     * @return true iff the CPU supports the 64-bit support
     * @since 0.9.21
     */
    public boolean hasX64() {
        return (CPUID.getExtendedEDXCPUFlags() & (1 << 29)) != 0; //Extended EDX Bit 29
    }
}
