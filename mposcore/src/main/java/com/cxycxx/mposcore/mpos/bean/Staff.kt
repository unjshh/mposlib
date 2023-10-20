package com.cxycxx.mposcore.mpos.bean

/**
 * 员工
 */
class Staff {

    /**
     * id
     */
    @JvmField
    var id = 0

    /**代码
     */
    @JvmField
    var code = ""

    /**
     * 姓名
     */
    @JvmField
    var name = ""

    /**
     * 部门id
     */
    @JvmField
    var deptId = 0

    /**
     * 部门代码
     */
    @JvmField
    var deptCode = ""

    /**
     * 部门名称
     */
    @JvmField
    var deptName = ""

    /**
     * 工作类型
     */
    var workType = ""
}