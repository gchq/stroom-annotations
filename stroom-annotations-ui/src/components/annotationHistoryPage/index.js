import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import AnnotationHistoryPage from './annotationHistoryPage'

import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'

export default connect(
    (state) => ({
        indexUuid: state.ui.indexUuid,
        annotationId: state.ui.annotationId,
        allowNavigation: state.ui.allowNavigation
    }),
    {
        fetchAnnotationHistory
    }
)(withRouter(AnnotationHistoryPage));
