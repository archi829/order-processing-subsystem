package com.erp.manufacturing.command;

import com.erp.manufacturing.repository.ManufacturingRepository;
import com.erp.model.dto.QCCheckDTO;

class SubmitQcCommand implements ManufacturingCommand<QCCheckDTO> {

    private final ManufacturingRepository repository;
    private final QCCheckDTO qcCheck;

    SubmitQcCommand(ManufacturingRepository repository, QCCheckDTO qcCheck) {
        this.repository = repository;
        this.qcCheck = qcCheck;
    }

    @Override
    public QCCheckDTO execute() {
        if (qcCheck.getQcCheckId() == null || qcCheck.getQcCheckId().trim().isEmpty()) {
            qcCheck.setQcCheckId(repository.nextId("mfg_quality_control", "qc_check_id", "QC-"));
        }
        return repository.insertQCCheck(qcCheck);
    }
}
