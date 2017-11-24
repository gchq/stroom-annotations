import { connect } from 'react-redux'

import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'

import History from './history'

export default connect(
    (state) => ({
        indexUuid: state.ui.indexUuid,
        annotationHistory: state.history
    }),
    {
        fetchAnnotationHistory
    }
)(History)
